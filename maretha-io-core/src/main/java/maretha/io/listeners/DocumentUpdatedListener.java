package maretha.io.listeners;

import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.CoreInstance;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.NuxeoException;
import org.nuxeo.ecm.core.api.model.Property;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.core.schema.types.primitives.DateType;
import org.nuxeo.ecm.platform.filemanager.service.FileManagerService;
import org.nuxeo.retention.adapters.Record;
import org.nuxeo.retention.adapters.RetentionRule;

public class DocumentUpdatedListener implements EventListener {

    private static final Log log = LogFactory.getLog(DocumentUpdatedListener.class);

    protected FileManagerService fileManagerService;

    @Override
    public void handleEvent(Event event) {
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
            return;
        }

        DocumentEventContext docCtx = (DocumentEventContext) ctx;
        DocumentModel document = docCtx.getSourceDocument();

        if (!"StudentFile".equals(document.getType())) {
            // ignore the event for System documents
            return;
        }

        Record record = document.getAdapter(Record.class);
        if (record == null) {
            log.debug("Document is NOT under legal retention, must attach  rule");
        }
        RetentionRule rule = record.getRule(ctx.getCoreSession());
        if (!rule.isMetadataBased()) {
            log.debug("No metdata rule found");
            return;

        }
        String xpath = rule.getMetadataXpath();
        if (StringUtils.isBlank(xpath)) {
            throw new NuxeoException("Metadata field is null");
        }

        Property prop = document.getProperty(xpath);
        if (!(prop.getType() instanceof DateType)) {
            throw new NuxeoException(
                    String.format("Field %s of type %s is expected to have a DateType", xpath, prop.getType()));
        }
        if (!prop.isDirty()) {
            log.debug("Prop" + xpath + " has not been modified");
            return;
        }
        CoreInstance.doPrivileged(docCtx.getCoreSession(), (session) -> {
            final Calendar retainUntil;
            Calendar value = (Calendar) prop.getValue();
            if (value != null) {
                Calendar retainUntilCandidate = rule.getRetainUntilDateFrom(value);
                Calendar now = Calendar.getInstance();
                if (now.after(retainUntilCandidate)) {
                    log.info("Retain date in the past, ignoring");
                    retainUntil = null;
                } else {
                    retainUntil = retainUntilCandidate;
                }
            } else {
                retainUntil = null;
            }
            if (retainUntil != null) {
                session.setRetainUntil(document.getRef(), retainUntil, null);
            }
        });
    }

}

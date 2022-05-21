package maretha.io.listeners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.EventContext;
import org.nuxeo.ecm.core.event.EventListener;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;
import org.nuxeo.ecm.platform.filemanager.service.FileManagerService;


public class RuleAttachedListener implements EventListener {

    private static final Log log = LogFactory.getLog(RuleAttachedListener.class);

    protected FileManagerService fileManagerService;

    @Override
    public void handleEvent(Event event) {
        EventContext ctx = event.getContext();
        if (!(ctx instanceof DocumentEventContext)) {
            return;
        }

        DocumentEventContext docCtx = (DocumentEventContext) ctx;
        DocumentModel doc = docCtx.getSourceDocument();

     

        if (!"StudentFile".equals(doc.getType())) {
            // ignore the event for System documents
            return;
        }

        // do something on doc as system user
        doc.addFacet("Record");
        docCtx.getCoreSession().setLegalHold(doc.getRef(), true, "automatically set under legal hold when a rule is attached");
        doc =   docCtx.getCoreSession().saveDocument(doc);
    }

}

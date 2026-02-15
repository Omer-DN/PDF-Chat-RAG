package org.handson.ragllm.model;

import java.time.LocalDateTime;

/**
 * Projection לרשימת קבצים – בלי שדה data (ה-LOB) כדי למנוע "Unable to access lob stream".
 */
public interface PdfFileSummary {

    Long getId();
    String getFilename();
    LocalDateTime getUploadedAt();
}

package org.handson.ragllm.model;
import com.pgvector.PGvector;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pdf_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdfFileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fileName;

    private LocalDateTime uploadedAt;

    @Column(name = "embedding", columnDefinition = "vector(1536)")
    private PGvector embedding; // נכון ל-pgvector
}

package com.trackademy.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "silabo_pdf_asset")
public class SilaboPdfAssetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(name = "storage_provider", nullable = false)
    public String storageProvider;

    @Column(name = "storage_key", nullable = false)
    public String storageKey;

    @Column(name = "original_filename", nullable = false)
    public String originalFilename;

    @Column(name = "mime_type", nullable = false)
    public String mimeType;

    @Column(name = "size_bytes")
    public Long sizeBytes;

    @Column(name = "sha256", nullable = false)
    public String sha256;

    @Column(name = "source_path")
    public String sourcePath;

    @Column(name = "created_at", nullable = false)
    public OffsetDateTime createdAt;
}

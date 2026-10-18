# Multimodal Models and Their Role in RAG

## 1. What is a multimodal model?

A multimodal model can understand or process more than one kind of input or output, such as text, images, audio, and video. The exact modalities depend on the specific model and API.

Examples include vision-capable models from OpenAI, Anthropic Claude, and Google Gemini. Check the chosen model's current documentation for supported input types, limits, and API availability.

## 2. Multimodal LLM vs. embedding model

These components solve different problems:

| Component | Main responsibility | Example use |
|---|---|---|
| Multimodal LLM | Interpret supported media and reason about its contents | Explain a UI screenshot, extract labels from a diagram, summarize a slide |
| Text embedding model | Convert text into a vector for semantic retrieval | Embed Markdown chunks and text extracted from HTML |
| Multimodal embedding model | Represent supported modalities in a shared or compatible vector space | Retrieve images using text queries, if the model supports cross-modal retrieval |
| Reranker / cross-encoder | Score query-document relevance | Reorder the top retrieved text passages |
| Vector database/index | Store and search vectors | Retrieve similar chunks using approximate nearest-neighbor search |

A multimodal LLM is not automatically a multimodal embedding model. A model that can describe an image does not necessarily create vectors suitable for image-to-text similarity search.

## 3. Where it fits in a RAG ingestion pipeline

For a knowledge base containing source code, Markdown, HTML, PDFs, screenshots, and architecture diagrams:

1. **Discover and parse sources.** Extract text, headings, tables, links, metadata, and code from text-oriented files.
2. **Process visual content.** Send supported screenshots or diagrams to a vision-capable model to extract visible text, component names, relationships, and a concise description.
3. **Validate extracted facts.** Preserve the source reference and distinguish explicit evidence from model interpretation. Do not treat a guessed arrow, dependency, or navigation path as confirmed.
4. **Create searchable records.** Chunk the extracted text and descriptions. Attach useful metadata such as source URI, document ID, asset type, section, and extraction method.
5. **Generate embeddings.** Use the selected embedding model to vectorize the text chunks or descriptions.
6. **Retrieve and answer.** Combine vector and lexical retrieval when appropriate, rerank candidates if needed, and generate an answer grounded in retrieved evidence.

### Simplified architecture

```text
Source files
  ├── Markdown / HTML / code ──> Parsers ─────────┐
  └── Screenshots / diagrams ──> Vision model ────┤
                                                  v
                                      Extracted text + metadata
                                                  |
                                            Chunking
                                                  |
                                         Embedding model
                                                  |
                                      Vector + lexical indexes
                                                  |
                                             Retrieval
                                                  |
                                      Grounded LLM response
```

## 4. Using a text embedding model with visual content

A text embedding model such as `sentence-transformers/all-mpnet-base-v2` embeds text; it does not directly understand raw image pixels. Its output is a 768-dimensional vector.

A practical approach is to use a vision-capable model to describe a screenshot or diagram, then embed that textual description with the existing text embedding model. This makes the extracted description searchable through text-based RAG, but it is not the same as direct image-vector retrieval.

Use a multimodal embedding model only if there is a clear requirement for cross-modal retrieval—for example, finding an image from a natural-language query—and verify that the chosen model supports the required modalities and indexing workflow.

## 5. Example: understanding a UI screenshot

Suppose a screenshot contains a page title, a Submit button, a customer-details form, and a link to a review screen.

A vision-capable model might extract:

- Screen name: Customer Details
- Visible controls: customer form, Submit button
- Visible navigation: link to Review
- Evidence: screenshot reference and extracted labels
- Uncertainty: backend API and service dependencies are not visible in the screenshot

Store the description and source reference. Do not infer backend calls or service-to-service relationships unless supported by code, configuration, traces, or another validated source.

## 6. Traceability and data quality

Store provenance alongside extracted knowledge, for example:

- `document_id` and `source_uri`
- `asset_type` (Markdown, HTML, screenshot, diagram, code)
- `extraction_method` and model/version, where appropriate
- `section_or_page`
- `evidence_status` (explicit, derived, inferred, unknown)
- ingestion timestamp and content hash

The source URI is not required for vector similarity calculations, but it is valuable for citations, debugging, re-ingestion, and opening the original source.

Keep extracted facts separate from inferred relationships. Review important relationships before using them as authoritative architecture data.

## 7. When is a multimodal model needed?

Use one when the source contains information that cannot be reliably extracted by ordinary text parsing, such as:

- UI screenshots and screen layouts
- Architecture diagrams and flowcharts
- Images embedded in documents
- Slides where layout contributes meaning
- Supported audio or video content

If the source already contains accessible text, HTML, or structured metadata, a standard parser may be simpler, cheaper, and more deterministic than a vision model.

## Key takeaways

- Multimodal LLMs interpret supported media; embedding models create vectors for retrieval.
- Existing text embeddings can index text extracted from images, but do not directly encode raw image pixels.
- Direct image-to-text vector retrieval requires an embedding model that supports the relevant modalities.
- Preserve source references and validate extracted relationships.
- Measure extraction quality and retrieval quality before changing the embedding architecture.

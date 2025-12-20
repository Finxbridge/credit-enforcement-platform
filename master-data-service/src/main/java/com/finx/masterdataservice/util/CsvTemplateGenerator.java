package com.finx.masterdataservice.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class CsvTemplateGenerator {

    /**
     * Generate CSV template with headers only
     */
    public byte[] generateTemplate(List<String> headers) {
        return generateTemplate(headers, null);
    }

    /**
     * Generate CSV template with headers and sample rows
     */
    public byte[] generateTemplate(List<String> headers, List<List<String>> sampleRows) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8)) {

            writer.write(String.join(",", headers));
            writer.write("\n");

            if (sampleRows != null && !sampleRows.isEmpty()) {
                for (List<String> row : sampleRows) {
                    writer.write(String.join(",", row));
                    writer.write("\n");
                }
            }

            writer.flush();
            return baos.toByteArray();

        } catch (IOException e) {
            log.error("Error generating CSV template", e);
            throw new RuntimeException("Failed to generate CSV template", e);
        }
    }

    /**
     * Generate master data upload CSV template (V1 - with type as query param)
     * Headers: code, value, displayOrder, isActive
     */
    public byte[] generateMasterDataTemplateV1(boolean includeSample) {
        List<String> headers = List.of(
                "code",
                "value",
                "displayOrder",
                "isActive"
        );

        if (!includeSample) {
            return generateTemplate(headers);
        }

        List<List<String>> sampleRows = List.of(
                List.of(
                        "ACTIVE",
                        "Active",
                        "1",
                        "true"
                ),
                List.of(
                        "INACTIVE",
                        "Inactive",
                        "2",
                        "true"
                )
        );

        return generateTemplate(headers, sampleRows);
    }

    /**
     * Generate master data upload CSV template (V2 - with categoryType in CSV)
     * Headers: categoryType, code, value, displayOrder, isActive
     */
    public byte[] generateMasterDataTemplateV2(boolean includeSample) {
        List<String> headers = List.of(
                "categoryType",
                "code",
                "value",
                "displayOrder",
                "isActive"
        );

        if (!includeSample) {
            return generateTemplate(headers);
        }

        List<List<String>> sampleRows = List.of(
                List.of(
                        "STATUS",
                        "ACTIVE",
                        "Active",
                        "1",
                        "true"
                ),
                List.of(
                        "STATUS",
                        "INACTIVE",
                        "Inactive",
                        "2",
                        "true"
                ),
                List.of(
                        "PRIORITY",
                        "HIGH",
                        "High Priority",
                        "1",
                        "true"
                )
        );

        return generateTemplate(headers, sampleRows);
    }
}

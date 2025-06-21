package com.udom.votingapplication.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.udom.votingapplication.models.Election;
import com.udom.votingapplication.models.Voter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {

    public byte[] generateVotingHistoryPdf(Voter voter, List<Election> votedElections) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Add title
            Paragraph title = new Paragraph("Voting History Report")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            // Add voter information
            Paragraph voterInfo = new Paragraph("Voter: " + voter.getFullName())
                    .setFontSize(12)
                    .setMarginTop(20);
            document.add(voterInfo);

            Paragraph username = new Paragraph("Username: " + voter.getUsername())
                    .setFontSize(12);
            document.add(username);

            Paragraph generateDate = new Paragraph("Generated: " + 
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .setFontSize(10)
                    .setMarginBottom(20);
            document.add(generateDate);

            if (votedElections.isEmpty()) {
                Paragraph noVotes = new Paragraph("No voting history found.")
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30);
                document.add(noVotes);
            } else {
                // Create table
                Table table = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 2}))
                        .useAllAvailableWidth();

                // Add headers
                table.addHeaderCell(new Cell().add(new Paragraph("Election Name").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Type").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("End Date").setBold()));
                table.addHeaderCell(new Cell().add(new Paragraph("Status").setBold()));

                // Add data rows
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                for (Election election : votedElections) {
                    election.calculateStatus();
                    
                    table.addCell(new Cell().add(new Paragraph(election.getName())));
                    table.addCell(new Cell().add(new Paragraph(
                        election.getVotingType() != null ? election.getVotingType() : "N/A")));
                    table.addCell(new Cell().add(new Paragraph(
                        election.getEndTime() != null ? election.getEndTime().format(formatter) : "N/A")));
                    table.addCell(new Cell().add(new Paragraph(election.getStatus())));
                }

                document.add(table);

                // Add summary
                Paragraph summary = new Paragraph("\nSummary:")
                        .setBold()
                        .setMarginTop(20);
                document.add(summary);

                Paragraph totalVotes = new Paragraph("Total Elections Participated: " + votedElections.size())
                        .setMarginLeft(10);
                document.add(totalVotes);
            }

            // Add footer
            Paragraph footer = new Paragraph("This document was generated automatically by the Voting System.")
                    .setFontSize(8)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30);
            document.add(footer);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF: " + e.getMessage(), e);
        }
    }
}

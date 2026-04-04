package com.railyatra.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.railyatra.entity.Booking;
import com.railyatra.entity.Passenger;
import com.railyatra.exception.ResourceNotFoundException;
import com.railyatra.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class PDFService {

    private final BookingRepository bookingRepository;

    public byte[] generateTicket(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
            .orElseThrow(() -> new ResourceNotFoundException("Booking not found: " + bookingId));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            Document doc = new Document(PageSize.A4);
            PdfWriter.getInstance(doc, baos);
            doc.open();

            Font titleFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.WHITE);
            Font headFont   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.DARK_GRAY);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11, Color.BLACK);
            Font smallFont  = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.GRAY);
            Font pnrFont    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 26, new Color(26, 86, 219));

            // Header
            PdfPTable header = new PdfPTable(1);
            header.setWidthPercentage(100);
            PdfPCell hCell = new PdfPCell(new Phrase("🚂  RailYatra — E-Ticket", titleFont));
            hCell.setBackgroundColor(new Color(26, 86, 219));
            hCell.setPadding(14); hCell.setBorder(Rectangle.NO_BORDER);
            hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            header.addCell(hCell);
            doc.add(header);
            doc.add(Chunk.NEWLINE);

            // Status
            String statusText = switch (booking.getStatus().name()) {
                case "CONFIRMED"  -> "✅  CONFIRMED";
                case "WAITLISTED" -> "⏳  WAITLISTED";
                default           -> "❌  CANCELLED";
            };
            Color statusColor = booking.getStatus().name().equals("CONFIRMED")
                ? new Color(21, 128, 61) : new Color(180, 83, 9);
            Font statusFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, statusColor);
            doc.add(new Paragraph("Status: " + statusText, statusFont));
            doc.add(Chunk.NEWLINE);

            // PNR
            Paragraph pnrPara = new Paragraph("PNR:  " + booking.getPnr(), pnrFont);
            pnrPara.setAlignment(Element.ALIGN_CENTER);
            doc.add(pnrPara);
            doc.add(Chunk.NEWLINE);

            // Train details
            PdfPTable details = new PdfPTable(2);
            details.setWidthPercentage(100);
            details.setSpacingBefore(8);
            addRow(details, "Train",      booking.getTrain().getTrainName() + " (" + booking.getTrain().getTrainNumber() + ")", headFont, normalFont);
            addRow(details, "From",       booking.getTrain().getSourceStation(), headFont, normalFont);
            addRow(details, "To",         booking.getTrain().getDestStation(),   headFont, normalFont);
            addRow(details, "Date",       booking.getJourneyDate().toString(),   headFont, normalFont);
            addRow(details, "Departure",  booking.getTrain().getDepartureTime().toString(), headFont, normalFont);
            addRow(details, "Arrival",    booking.getTrain().getArrivalTime().toString(),   headFont, normalFont);
            addRow(details, "Class",      booking.getClassType(),                headFont, normalFont);
            if (booking.getWaitlistNumber() != null)
                addRow(details, "WL No.", "WL/" + booking.getWaitlistNumber(), headFont, normalFont);
            doc.add(details);
            doc.add(Chunk.NEWLINE);

            // Passengers
            doc.add(new Paragraph("Passengers", headFont));
            doc.add(Chunk.NEWLINE);
            PdfPTable pTable = new PdfPTable(5);
            pTable.setWidthPercentage(100);
            pTable.setWidths(new float[]{3f, 1f, 1.5f, 2f, 2f});
            for (String col : new String[]{"Name","Age","Gender","Seat","Berth Pref"}) {
                PdfPCell c = new PdfPCell(new Phrase(col, headFont));
                c.setBackgroundColor(new Color(243, 244, 246)); c.setPadding(7);
                pTable.addCell(c);
            }
            for (Passenger p : booking.getPassengers()) {
                pTable.addCell(new Phrase(p.getName(), normalFont));
                pTable.addCell(new Phrase(String.valueOf(p.getAge()), normalFont));
                pTable.addCell(new Phrase(p.getGender(), normalFont));
                pTable.addCell(new Phrase(p.getSeatNumber() != null ? p.getSeatNumber() : "TBA", normalFont));
                pTable.addCell(new Phrase(p.getBerthPref() != null ? p.getBerthPref() : "-", normalFont));
            }
            doc.add(pTable);
            doc.add(Chunk.NEWLINE);

            // Fare
            PdfPTable fare = new PdfPTable(2);
            fare.setWidthPercentage(45);
            fare.setHorizontalAlignment(Element.ALIGN_RIGHT);
            addRow(fare, "Base Fare",        "₹" + booking.getTotalAmount().subtract(booking.getConvenienceFee()), headFont, normalFont);
            addRow(fare, "Convenience Fee",  "₹" + booking.getConvenienceFee(), headFont, normalFont);
            Font totalFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new Color(26, 86, 219));
            addRow(fare, "Total Paid",        "₹" + booking.getTotalAmount(), headFont, totalFont);
            doc.add(fare);
            doc.add(Chunk.NEWLINE);

            // Footer
            Paragraph footer = new Paragraph(
                "Carry a valid photo ID. This is your official e-ticket. — RailYatra 🚀", smallFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();
        } catch (Exception e) {
            log.error("PDF generation failed: {}", e.getMessage());
            throw new RuntimeException("Failed to generate ticket PDF", e);
        }
        return baos.toByteArray();
    }

    private void addRow(PdfPTable t, String label, String value, Font lf, Font vf) {
        PdfPCell lc = new PdfPCell(new Phrase(label, lf));
        lc.setPadding(7); lc.setBackgroundColor(new Color(249, 250, 251));
        t.addCell(lc);
        PdfPCell vc = new PdfPCell(new Phrase(value, vf));
        vc.setPadding(7);
        t.addCell(vc);
    }
}

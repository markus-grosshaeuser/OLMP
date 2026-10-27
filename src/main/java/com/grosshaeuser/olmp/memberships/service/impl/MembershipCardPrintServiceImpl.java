package com.grosshaeuser.olmp.memberships.service.impl;

import com.grosshaeuser.olmp.members.dto.MemberEditDTO;
import com.grosshaeuser.olmp.memberships.service.MembershipBarcodeService;
import com.grosshaeuser.olmp.memberships.dto.MembershipEditDTO;
import com.grosshaeuser.olmp.memberships.service.MembershipCardPrintService;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Objects;

@Service
public class MembershipCardPrintServiceImpl implements MembershipCardPrintService {
    private final MembershipBarcodeService barcodeService;

    public MembershipCardPrintServiceImpl(MembershipBarcodeService barcodeService) {
        this.barcodeService = barcodeService;
    }

    @Override
    public String createPrintDocument(MemberEditDTO member, MembershipEditDTO membership) {
        Objects.requireNonNull(member, "Member must not be null");
        Objects.requireNonNull(membership, "Membership must not be null");

        if (member.getFirstName() == null || member.getFirstName().isEmpty() || member.getLastName() == null || member.getLastName().isEmpty()) {
            throw new IllegalArgumentException("Member first name must not be null or empty");
        }

        if (membership.getMembershipNumber() == null || membership.getMembershipNumber().isEmpty()) {
            throw new IllegalArgumentException("Membership number must not be null or empty");
        }

        if (membership.getMembershipType() == null || membership.getMembershipType().isEmpty()) {
            throw new IllegalArgumentException("Membership type must not be null or empty");
        }

        if (membership.getStartDate() == null || membership.getExpirationDate() == null) {
            throw new IllegalArgumentException("Membership start date and expiration date must not be null");
        }

        String barcode = Base64.getEncoder().encodeToString(barcodeService.generateBarcodePng(membership.getMembershipNumber()));

        return String.format("""
                    <!doctype html>
                    <html lang="en">
                    <head>
                    <meta charset="utf-8">
                    <style>
                        @page { size: A4; margin: 10mm; }
                        body { margin: 0; padding: 0; background: white; font-family: Arial, sans-serif; }
                    
                        .card {
                            width: 86mm;
                            height: 54mm;
                            display: flex;
                            flex-direction: column;
                            justify-content: space-between;
                            box-sizing: border-box;
                            padding: 0.2mm;
                            border: 0.1mm solid #eee; /* Light border for visualization, can be removed */
                            border-radius: 15px;
                            overflow: hidden;
                        }
                    
                        .info {
                            flex-grow: 1;
                            padding: 5mm;
                        }
                    
                        .info h1 {
                            margin: 0 0 2mm 0;
                            font-size: 14pt;
                            color: #333;
                        }
                    
                        .info p {
                            margin: 1mm 0;
                            font-size: 9pt;
                            color: #555;
                        }

                        .info strong {
                            font-size: 11pt;
                            color: black;
                        }

                        .barcode-container {
                            width: 100%%;
                            height: 14mm;
                            display: flex;
                            align-items: flex-end;
                        }

                        .barcode-container img {
                            width: 100%%;
                            height: auto;
                            display: block;
                        }
                    </style>
                    </head>
                    <body>
                    <div class="card">
                        <div class="info">
                            <h1>Membership Card</h1>
                            <p><strong>%s %s</strong></p>
                            <p>No: %s</p>
                            <p>Type: %s</p>
                            <p>Expires: %s</p>
                        </div>
                        <div class="barcode-container">
                            <img src="data:image/png;base64,%s" alt="Barcode">
                        </div>
                    </div>
                    </body>
                    </html>
                    """,
                member.getFirstName(),
                member.getLastName(),
                membership.getMembershipNumber(),
                membership.getMembershipType(),
                membership.getExpirationDate(),
                barcode
        );
    }
}

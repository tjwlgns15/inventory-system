package com.yhs.inventroysystem.application.contract;

import java.time.LocalDate;

public class ContractCommands {

    public record CreateCommand(
            LocalDate recordDate,
            String documentName,
            String companyName,
            LocalDate contractDate,
            LocalDate validityStartDate,
            LocalDate validityEndDate,
            LocalDate signedDate,
            String signerName,
            String note
    ) {}

    public record UpdateCommand(
            LocalDate recordDate,
            String documentName,
            String companyName,
            LocalDate contractDate,
            LocalDate validityStartDate,
            LocalDate validityEndDate,
            LocalDate signedDate,
            String signerName,
            String note
    ) {}
}
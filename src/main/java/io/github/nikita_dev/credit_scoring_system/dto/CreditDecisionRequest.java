package io.github.nikita_dev.credit_scoring_system.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreditDecisionRequest {

    @NotNull(message = "Loan amount cannot be null")
    @Positive(message = "Loan amount must be positive")
    @JsonProperty("loan_amnt")
    private Double loanAmnt;

    @NotNull(message = "Term cannot be null")
    @Positive(message = "Term must be positive")
    private Integer term;

    @NotNull(message = "Interest rate cannot be null")
    @Positive(message = "Interest rate must be positive")
    @JsonProperty("int_rate")
    private Double intRate;

    @NotBlank(message = "Grade cannot be blank")
    private String grade;

    @NotNull(message = "Employment length cannot be null")
    @Min(value = 0, message = "Employment length cannot be negative")
    @JsonProperty("emp_length")
    private Integer empLength;

    @NotBlank(message = "Home ownership cannot be blank")
    @JsonProperty("home_ownership")
    private String homeOwnership;

    @NotNull(message = "Annual income cannot be null")
    @Positive(message = "Annual income must be positive")
    @JsonProperty("annual_inc")
    private Double annualInc;

    @NotBlank(message = "Verification status cannot be blank")
    @JsonProperty("verification_status")
    private String verificationStatus;

    @NotBlank(message = "Purpose cannot be blank")
    private String purpose;

    @NotNull(message = "DTI cannot be null")
    @Min(value = 0, message = "DTI cannot be negative")
    private Double dti;

    @NotNull(message = "FICO range low cannot be null")
    @Positive(message = "FICO range low must be positive")
    @JsonProperty("fico_range_low")
    private Integer ficoRangeLow;

    @NotNull(message = "FICO range high cannot be null")
    @Positive(message = "FICO range high must be positive")
    @JsonProperty("fico_range_high")
    private Integer ficoRangeHigh;

    @NotNull(message = "Open accounts cannot be null")
    @Min(value = 0, message = "Open accounts cannot be negative")
    @JsonProperty("open_acc")
    private Integer openAcc;

    @NotNull(message = "Public records cannot be null")
    @Min(value = 0, message = "Public records cannot be negative")
    @JsonProperty("pub_rec")
    private Integer pubRec;

    @NotNull(message = "Revolving balance cannot be null")
    @Min(value = 0, message = "Revolving balance cannot be negative")
    @JsonProperty("revol_bal")
    private Double revolBal;

    @NotNull(message = "Revolving utilization cannot be null")
    @Min(value = 0, message = "Revolving utilization cannot be negative")
    @JsonProperty("revol_util")
    private Double revolUtil;

    @NotNull(message = "Total accounts cannot be null")
    @Min(value = 0, message = "Total accounts cannot be negative")
    @JsonProperty("total_acc")
    private Integer totalAcc;

    @NotBlank(message = "Initial list status cannot be blank")
    @JsonProperty("initial_list_status")
    private String initialListStatus;

    @NotBlank(message = "Application type cannot be blank")
    @JsonProperty("application_type")
    private String applicationType;
}
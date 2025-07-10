package io.github.nikita_dev.credit_scoring_system.service;

import io.github.nikita_dev.credit_scoring_system.dto.CreditDecisionRequest;
import io.github.nikita_dev.credit_scoring_system.dto.CreditDecisionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tribuo.Example;
import org.tribuo.ImmutableFeatureMap;
import org.tribuo.Model;
import org.tribuo.Prediction;
import org.tribuo.classification.Label;
import org.tribuo.impl.ArrayExample;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionService {

    private final Model<Label> model;

    public CreditDecisionResponse predict(CreditDecisionRequest request) {
        Example<Label> example = buildExample(request);

        Prediction<Label> prediction = model.predict(example);
        String decision = prediction.getOutput().getLabel().equals("1")
                ? "APPROVED" : "REJECTED";

        return new CreditDecisionResponse(decision,
                prediction.getOutput().getScore());
    }


    private Example<Label> buildExample(CreditDecisionRequest r) {
        ArrayExample<Label> ex = new ArrayExample<>(new Label("UNKNOWN"));

        addNum(ex, "loan_amnt",        r.getLoanAmnt());
        addNum(ex, "term",             r.getTerm());
        addNum(ex, "int_rate",         r.getIntRate());
        addNum(ex, "annual_inc",       r.getAnnualInc());
        addNum(ex, "dti",              r.getDti());
        addNum(ex, "fico_range_low",   r.getFicoRangeLow());
        addNum(ex, "fico_range_high",  r.getFicoRangeHigh());
        addNum(ex, "open_acc",         r.getOpenAcc());
        addNum(ex, "pub_rec",          r.getPubRec());
        addNum(ex, "revol_bal",        r.getRevolBal());
        addNum(ex, "revol_util",       r.getRevolUtil());
        addNum(ex, "total_acc",        r.getTotalAcc());
        addNum(ex, "emp_length",       r.getEmpLength());

        addCat(ex, "grade",               r.getGrade());
        addCat(ex, "home_ownership",      r.getHomeOwnership());
        addCat(ex, "verification_status", r.getVerificationStatus());
        addCat(ex, "purpose",             r.getPurpose());
        addCat(ex, "initial_list_status", r.getInitialListStatus());
        addCat(ex, "application_type",    r.getApplicationType());

        return ex;
    }

    private void addNum(ArrayExample<Label> ex, String name, Number v) {
        if (v != null) ex.add(name + "@value", v.doubleValue());
    }

    private void addCat(ArrayExample<Label> ex, String name, String v) {
        if (v != null && !v.isBlank()) ex.add(name + "@" + v, 1.0);
    }
}
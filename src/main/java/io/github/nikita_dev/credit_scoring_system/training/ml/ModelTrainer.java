package io.github.nikita_dev.credit_scoring_system.training.ml;

import lombok.extern.slf4j.Slf4j;
import org.tribuo.Example;
import org.tribuo.Model;
import org.tribuo.MutableDataset;
import org.tribuo.classification.Label;
import org.tribuo.classification.evaluation.LabelEvaluation;
import org.tribuo.classification.evaluation.LabelEvaluator;
import org.tribuo.classification.xgboost.XGBoostClassificationTrainer;
import org.tribuo.protos.core.ModelProto;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static io.github.nikita_dev.credit_scoring_system.training.ml.TrainerFactory.createXGBoostTrainer;

@Slf4j
public class ModelTrainer {


    public void trainFinalModel(
            MutableDataset<Label> trainData,
            MutableDataset<Label> testData,
            Map<String, Object> bestParams,
            Path modelOutputPath
    ) throws IOException {

        applyClassWeights(trainData);

        XGBoostClassificationTrainer trainer = createXGBoostTrainer(bestParams);

        log.info("Training final model with best parameters...");
        Model<Label> model = trainer.train(trainData);
        log.info("Final model training completed successfully.");

        evaluateAndLog(model, testData);

        saveModel(model, modelOutputPath);
    }

    private void applyClassWeights(MutableDataset<Label> trainData) {
        log.info("Applying weights to handle class imbalance...");

        Map<String, Long> classCounts = new HashMap<>();
        for (Example<Label> example : trainData) {
            String label = example.getOutput().getLabel();
            classCounts.merge(label, 1L, Long::sum);
        }

        long countClass0 = classCounts.getOrDefault("0", 0L);
        long countClass1 = classCounts.getOrDefault("1", 0L);
        log.debug("Class distribution in training data: Class '0': {}, Class '1': {}", countClass0, countClass1);

        if (countClass0 == 0 || countClass1 == 0) {
            log.warn("One of the classes has zero examples, skipping weighting.");
            return;
        }

        String minorityClassLabel;
        float weight;

        if (countClass0 < countClass1) {
            minorityClassLabel = "0";
            weight = (float) countClass1 / countClass0;
        } else {
            minorityClassLabel = "1";
            weight = (float) countClass0 / countClass1;
        }

        log.debug("Calculated weight for minority class '{}' is: {}", minorityClassLabel, weight);

        for (Example<Label> example : trainData) {
            if (example.getOutput().getLabel().equals(minorityClassLabel)) {
                example.setWeight(weight);
            }
        }
        log.info("Weights applied successfully.");
    }

    private void evaluateAndLog(Model<Label> model, MutableDataset<Label> testData) {
        log.info("Starting final model evaluation...");
        LabelEvaluator evaluator = new LabelEvaluator();
        LabelEvaluation evaluation = evaluator.evaluate(model, testData);

        log.info("--- FINAL MODEL EVALUATION RESULTS ---");
        log.info("Accuracy: {}", evaluation.accuracy());
        log.info("F1-Score (Class '0'): {}", evaluation.f1(new Label("0")));
        log.info("F1-Score (Class '1'): {}", evaluation.f1(new Label("1")));
        log.info("------------------------------------");
    }

    private void saveModel(Model<Label> model, Path path) throws IOException {
        log.info("Saving model to '{}' using Protobuf...", path);

        ModelProto proto = model.serialize();

        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path.toFile()))) {
            proto.writeTo(bos);
        }

        log.info("Model successfully saved in Protobuf format.");
    }
}
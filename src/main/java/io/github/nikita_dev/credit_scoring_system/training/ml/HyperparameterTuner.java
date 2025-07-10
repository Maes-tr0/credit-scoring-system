package io.github.nikita_dev.credit_scoring_system.training.ml;

import lombok.extern.slf4j.Slf4j;
import org.tribuo.Model;
import org.tribuo.MutableDataset;
import org.tribuo.classification.Label;
import org.tribuo.classification.LabelFactory;
import org.tribuo.classification.evaluation.LabelEvaluation;
import org.tribuo.classification.evaluation.LabelEvaluator;
import org.tribuo.classification.xgboost.XGBoostClassificationTrainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static io.github.nikita_dev.credit_scoring_system.training.ml.TrainerFactory.createXGBoostTrainer;

@Slf4j
public class HyperparameterTuner {

    private final MutableDataset<Label> trainData;
    private final MutableDataset<Label> testData;
    private final Random random = new Random();
    private final List<Integer> numTreesOptions = List.of(100, 200, 300);
    private final List<Integer> maxDepthOptions = List.of(5, 6, 8);
    private final List<Double> etaOptions = List.of(0.05, 0.1, 0.2);
    private double bestF1Score = -1.0;

    public HyperparameterTuner(MutableDataset<Label> trainData, MutableDataset<Label> testData) {
        this.trainData = trainData;
        this.testData = testData;
    }

    public Map<String, Object> tune() {
        Map<String, Object> bestParams = new HashMap<>();

        LabelFactory factory = new LabelFactory();
        Label minorityClassLabel = factory.generateOutput("0");

        int numberOfTrials = 10;
        log.info("Starting Random Search for {} trials...", numberOfTrials);

        for (int i = 0; i < numberOfTrials; i++) {
            log.debug("--- Trial {}/{} ---", i + 1, numberOfTrials);

            Map<String, Object> currentParams = new HashMap<>();
            currentParams.put("numTrees", numTreesOptions.get(random.nextInt(numTreesOptions.size())));
            currentParams.put("max_depth", maxDepthOptions.get(random.nextInt(maxDepthOptions.size())));
            currentParams.put("eta", etaOptions.get(random.nextInt(etaOptions.size())));

            XGBoostClassificationTrainer trainer = createXGBoostTrainer(currentParams);
            Model<Label> model = trainer.train(trainData);

            LabelEvaluation evaluation = new LabelEvaluator().evaluate(model, testData);
            double currentF1 = evaluation.f1(minorityClassLabel);

            log.debug("Params: {}. F1-Score(0): {}", currentParams, currentF1);

            if (currentF1 > bestF1Score) {
                bestF1Score = currentF1;
                bestParams = currentParams;
                log.debug("!!! New best F1 score found: {} !!!", bestF1Score);
            }
        }

        log.info("--- Random Search Finished ---");
        log.info("Best F1-Score for class '0' found: {}", bestF1Score);
        log.info("Best Hyperparameters found: {}", bestParams);

        return bestParams;
    }
}
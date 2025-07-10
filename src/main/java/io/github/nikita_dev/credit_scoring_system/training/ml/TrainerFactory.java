package io.github.nikita_dev.credit_scoring_system.training.ml;

import lombok.extern.slf4j.Slf4j;
import org.tribuo.classification.xgboost.XGBoostClassificationTrainer;

import java.util.Map;

@Slf4j
public final class TrainerFactory {
    private TrainerFactory() {}

    public static XGBoostClassificationTrainer createXGBoostTrainer(Map<String, Object> hyperparams) {
        log.debug("Configuring XGBoost trainer with params: {}", hyperparams);

        return new XGBoostClassificationTrainer(
                (Integer) hyperparams.getOrDefault("numTrees", 100),
                (Double) hyperparams.getOrDefault("eta", 0.1),
                0.0,
                (Integer) hyperparams.getOrDefault("max_depth", 5),
                1.0,
                0.7,
                0.8,
                1.0,
                0.0,
                4,
                true,
                1L
        );
    }
}
package io.github.nikita_dev.credit_scoring_system.training;

import com.opencsv.exceptions.CsvValidationException;
import io.github.nikita_dev.credit_scoring_system.training.data.DataCleaner;
import io.github.nikita_dev.credit_scoring_system.training.ml.HyperparameterTuner;
import io.github.nikita_dev.credit_scoring_system.training.ml.ModelTrainer;
import lombok.extern.slf4j.Slf4j;
import org.tribuo.MutableDataset;
import org.tribuo.classification.Label;
import org.tribuo.classification.LabelFactory;
import org.tribuo.data.columnar.FieldProcessor;
import org.tribuo.data.columnar.RowProcessor;
import org.tribuo.data.columnar.processors.field.DoubleFieldProcessor;
import org.tribuo.data.columnar.processors.field.IdentityProcessor;
import org.tribuo.data.columnar.processors.response.FieldResponseProcessor;
import org.tribuo.data.csv.CSVDataSource;
import org.tribuo.evaluation.TrainTestSplitter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TrainingLauncher {
    private static final Path RAW_DATA_PATH = Paths.get("src/main/resources/data/raw/Loan_status.csv");
    private static final Path CLEANED_DATA_PATH = Paths.get("src/main/resources/data/cleaned/Loan_status_cleaned.csv");
    private static final Path MODEL_OUTPUT_PATH = Paths.get("src/main/resources/model/credit-approval.pb");

    public static void main(String[] args) {
        try {
            new DataCleaner().cleanCsvFile(RAW_DATA_PATH, CLEANED_DATA_PATH);

            TrainTestSplitter<Label> splitter = loadAndSplitData();
            MutableDataset<Label> trainData = new MutableDataset<>(splitter.getTrain());
            MutableDataset<Label> testData = new MutableDataset<>(splitter.getTest());

            HyperparameterTuner tuner = new HyperparameterTuner(trainData, testData);
            Map<String, Object> bestParams = tuner.tune();

            ModelTrainer finalTrainer = new ModelTrainer();
            finalTrainer.trainFinalModel(trainData, testData, bestParams, MODEL_OUTPUT_PATH);

            log.info("Training pipeline completed successfully.");

        } catch (IOException | CsvValidationException e) {
            log.error("A critical error occurred during the training pipeline.", e);
            e.printStackTrace();
        }
    }

    private static TrainTestSplitter<Label> loadAndSplitData() {
        FieldResponseProcessor<Label> responseProcessor = new FieldResponseProcessor<>("is_approved", "0", new LabelFactory());

        Map<String, FieldProcessor> fieldProcessorMap = new HashMap<>();
        fieldProcessorMap.put("loan_amnt", new DoubleFieldProcessor("loan_amnt"));
        fieldProcessorMap.put("term", new DoubleFieldProcessor("term"));
        fieldProcessorMap.put("int_rate", new DoubleFieldProcessor("int_rate"));
        fieldProcessorMap.put("emp_length", new DoubleFieldProcessor("emp_length"));
        fieldProcessorMap.put("annual_inc", new DoubleFieldProcessor("annual_inc"));
        fieldProcessorMap.put("dti", new DoubleFieldProcessor("dti"));
        fieldProcessorMap.put("fico_range_low", new DoubleFieldProcessor("fico_range_low"));
        fieldProcessorMap.put("fico_range_high", new DoubleFieldProcessor("fico_range_high"));
        fieldProcessorMap.put("open_acc", new DoubleFieldProcessor("open_acc"));
        fieldProcessorMap.put("pub_rec", new DoubleFieldProcessor("pub_rec"));
        fieldProcessorMap.put("revol_bal", new DoubleFieldProcessor("revol_bal"));
        fieldProcessorMap.put("revol_util", new DoubleFieldProcessor("revol_util"));
        fieldProcessorMap.put("total_acc", new DoubleFieldProcessor("total_acc"));
        fieldProcessorMap.put("grade", new IdentityProcessor("grade"));
        fieldProcessorMap.put("home_ownership", new IdentityProcessor("home_ownership"));
        fieldProcessorMap.put("verification_status", new IdentityProcessor("verification_status"));
        fieldProcessorMap.put("purpose", new IdentityProcessor("purpose"));
        fieldProcessorMap.put("initial_list_status", new IdentityProcessor("initial_list_status"));
        fieldProcessorMap.put("application_type", new IdentityProcessor("application_type"));

        RowProcessor<Label> rowProcessor = new RowProcessor<>(responseProcessor, fieldProcessorMap);
        CSVDataSource<Label> dataSource = new CSVDataSource<>(CLEANED_DATA_PATH, rowProcessor, true);
        return new TrainTestSplitter<>(dataSource, 0.8, 1L);
    }
}
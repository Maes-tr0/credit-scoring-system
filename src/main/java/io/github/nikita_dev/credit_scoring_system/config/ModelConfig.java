package io.github.nikita_dev.credit_scoring_system.config;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.tribuo.Model;
import org.tribuo.classification.Label;
import org.tribuo.protos.core.ModelProto;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class ModelConfig {
    @Value("${application.model.path}")
    private Resource modelResource;


    @Bean
    public Model<Label> loadModel() {
        log.info("Loading Tribuo model from resource: {}", modelResource.getFilename());

        try (InputStream inputStream = modelResource.getInputStream()) {
            ModelProto proto = ModelProto.parseFrom(inputStream);
            log.debug("ModelProto parsed successfully. Version: {}, Size: {} bytes.",
                    proto.getVersion(),
                    proto.getSerializedSize()
            );

            Model<?> rawModel = Model.deserialize(proto);
            log.debug("Model deserialized from proto. Raw model type: {}", rawModel.getClass().getSimpleName());

            Model<Label> model = rawModel.castModel(Label.class);
            log.info("Credit scoring model loaded successfully. Type: {}", model.getClass().getSimpleName());

            return model;
        } catch (IOException e) {
            log.error("Failed to read model file from resource: {}", modelResource, e);
            throw new IllegalStateException("Unable to read Tribuo model file.", e);
        } catch (ClassCastException e){
            log.error("Model type mismatch. Failed to cast the deserialized model to Model<Label>.", e);
            throw new IllegalStateException("Could not cast model to the required type.", e);
        } catch (Exception e) {
            log.error("An unexpected error occurred while loading the Tribuo model.", e);
            throw new IllegalStateException("Unexpected error during model loading.", e);
        }
    }
}

package BE.service;

import BE.entity.Model;
import BE.repository.ModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ModelService {

    @Autowired
    private ModelRepository modelRepository;


    public Model createModel(Model model) {
        model.setModelID(null);
        return modelRepository.save(model);
    }

    public List<Model> getAllModels() {
        return modelRepository.findAll();
    }

    public Optional<Model> getModelById(Long modelID) {
        return modelRepository.findById(modelID);
    }

    // UPDATE - Cập nhật Model
    public Model updateModel(Long modelID, Model modelDetails) {
        Optional<Model> model = modelRepository.findById(modelID);
        if (model.isPresent()) {
            Model existingModel = model.get();
            if (modelDetails.getModelName() != null) {
                existingModel.setModelName(modelDetails.getModelName());
            }
            return modelRepository.save(existingModel);
        }
        return null;
    }

    public boolean deleteModel(Long modelID) {
        if (modelRepository.existsById(modelID)) {
            modelRepository.deleteById(modelID);
            return true;
        }
        return false;
    }

    public void deleteAllModels() {
        modelRepository.deleteAll();
    }
}
package BE.controller;

import BE.entity.Model;
import BE.model.DTO.ModelDTO;
import BE.model.response.ModelResponse;
import BE.service.ModelService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/model")
@RequiredArgsConstructor
@SecurityRequirement(name = "api")
public class ModelController {

    @Autowired
    private ModelService modelService;

    @Autowired
    ModelMapper modelMapper;

    @PostMapping(value="/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ModelResponse> createModel(@RequestPart("modelName") String modelName,
                                                     @RequestPart("image") MultipartFile image) throws Exception
    {
        ModelDTO modelDTO = new ModelDTO();
        modelDTO.setModelName(modelName);
        modelDTO.setImage(image);

        Model createdModel = modelService.createModel(modelDTO);
        return new ResponseEntity<>(modelMapper.map(createdModel, ModelResponse.class), HttpStatus.CREATED);
    }

    // READ - GET /api/models
    @GetMapping(name="/getAll")
    public ResponseEntity<List<ModelResponse>> getAllModels() {
        List<Model> models = modelService.getAllModels();
        List<ModelResponse> modelResponses = models.stream()
                .map(model -> modelMapper.map(model, ModelResponse.class))
                .toList();
        return new ResponseEntity<>(modelResponses, HttpStatus.OK);
    }

    // READ - GET /api/models/{id}
    @GetMapping("/getModelBy/{id}")
    public ResponseEntity<ModelResponse> getModelById(@PathVariable Long id) {
        Optional<Model> model = modelService.getModelById(id);
        return model.map(value -> new ResponseEntity<>(modelMapper.map(value, ModelResponse.class), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // UPDATE - PUT /api/models/{id}
    @PutMapping("/update/{id}")
    public ResponseEntity<ModelResponse> updateModel(@PathVariable Long id, @RequestBody ModelDTO modelDTO) {
        Model modelDetails = modelMapper.map(modelDTO, Model.class);
        Model updatedModel = modelService.updateModel(id, modelDetails);
        if (updatedModel != null) {
            return new ResponseEntity<>(modelMapper.map(updatedModel, ModelResponse.class), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // DELETE - DELETE /api/models/{id}
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteModel(@PathVariable Long id) {
        boolean deleted = modelService.deleteModel(id);
        if (deleted) {
            return new ResponseEntity<>("Model deleted successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("Model not found", HttpStatus.NOT_FOUND);
    }

   // DELETE - DELETE /api/models
//    @DeleteMapping
//    public ResponseEntity<String> deleteAllModels() {
//        modelService.deleteAllModels();
//        return new ResponseEntity<>("All models deleted successfully", HttpStatus.OK);
//    }

}


package BE.controller;

import BE.entity.Model;
import BE.model.DTO.ModelDTO;
import BE.model.response.ModelResponse;
import BE.service.ModelService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasAuthority('admin')")
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
    @PreAuthorize("isAuthenticated()")
    @GetMapping(name="/getAll")
    public ResponseEntity<List<ModelResponse>> getAllModels() {
        List<Model> models = modelService.getAllModels();
        List<ModelResponse> modelResponses = models.stream()
                .map(model -> modelMapper.map(model, ModelResponse.class))
                .toList();
        return new ResponseEntity<>(modelResponses, HttpStatus.OK);
    }

    // READ - GET /api/models/{id}
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/getModelBy/{id}")
    public ResponseEntity<ModelResponse> getModelById(@PathVariable Long id) {
        Optional<Model> model = modelService.getModelById(id);
        return model.map(value -> new ResponseEntity<>(modelMapper.map(value, ModelResponse.class), HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // UPDATE - PUT /api/models/{id}
    @PreAuthorize("hasAuthority('admin')")
    @PutMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ModelResponse> updateModel(@PathVariable("id") Long id,
                                                     @RequestPart(value = "modelName") String modelName,
                                                     @RequestPart(value = "image", required = false) MultipartFile image) throws Exception
    {
        ModelDTO modelDTO = new ModelDTO();
        modelDTO.setModelName(modelName);
        modelDTO.setImage(image);

        Model updatedModel = modelService.updateModel(id, modelDTO);
        if (updatedModel != null) {
            return new ResponseEntity<>(modelMapper.map(updatedModel, ModelResponse.class), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // DELETE - DELETE /api/models/{id}
    @PreAuthorize("hasAuthority('admin')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteModel(@PathVariable Long id) {
        boolean deleted = modelService.deleteModel(id);
        if (deleted) {
            return new ResponseEntity<>("Model deleted successfully", HttpStatus.OK);
        }
        return new ResponseEntity<>("Model not found", HttpStatus.NOT_FOUND);
    }

}


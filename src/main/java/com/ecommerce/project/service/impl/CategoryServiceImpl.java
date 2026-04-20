package com.ecommerce.project.service.impl;

import com.ecommerce.project.exception.APIException;
import com.ecommerce.project.exception.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDto;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.Repository.CategoryRepository;
import com.ecommerce.project.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private  final CategoryRepository repositry;
    private final ModelMapper modelMapper;

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")?Sort.by(sortBy).ascending():Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Category> categoryPage = repositry.findAll(pageDetails);

        List<Category> all = categoryPage.getContent();


        if (all.isEmpty()){
            throw new APIException("No category created till now ");
        }

        List<CategoryDto> categoryDtos = all.stream().map(category -> modelMapper.map(category,CategoryDto.class)).toList();

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDtos);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());
        return categoryResponse;
    }

    @Override
    public CategoryDto createCategory(CategoryDto categoryDto) {
        Category category = modelMapper.map(categoryDto,Category.class);

        Category byCategoryName = repositry.findByCategoryName(category.getCategoryName());

        if (byCategoryName != null){
            throw new APIException("Category with name\s"+ category.getCategoryName() + "\salready exists");
        }

        Category savedCategory = repositry.save(category);
        return modelMapper.map(savedCategory,CategoryDto.class);
    }

    @Override
    public CategoryDto deleteCategoryById(Long categoryId) {
//        Category category = categories.stream().filter(c->c.getCategoryId().equals(categoryId)).findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Resource Not found"));
//        categories.remove(category);


        Category category = repositry.findById(categoryId).orElseThrow(()->new ResourceNotFoundException("Category","categoryId",categoryId));
        repositry.deleteById(categoryId);
        CategoryDto dto = modelMapper.map(category,CategoryDto.class);
        return dto;
    }

    @Override
    public CategoryDto updateCategory(CategoryDto categoryDto, Long categoryId) {
        Category savedCategory = repositry.findById(categoryId).orElseThrow(()->new ResourceNotFoundException("Category","categoryId",categoryId));

        Category category = modelMapper.map(categoryDto,Category.class);
        category.setCategoryId(categoryId);
       savedCategory = repositry.save(category);

        return modelMapper.map(savedCategory,CategoryDto.class);
    }
}

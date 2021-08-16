package com.epam.esm.webservice.service.impl;

import com.epam.esm.webservice.config.ServiceUtilConfig;
import com.epam.esm.webservice.dto.CertificateDTO;
import com.epam.esm.webservice.entity.Certificate;
import com.epam.esm.webservice.repository.CertificateRepository;
import com.epam.esm.webservice.service.CertificateService;
import com.epam.esm.webservice.service.exception.CertificateNotFoundException;
import com.epam.esm.webservice.service.exception.InvalidPageNumberException;
import com.epam.esm.webservice.service.exception.ResourceNotFoundException;
import com.epam.esm.webservice.util.CertificateParameters;
import com.epam.esm.webservice.util.Pagination;
import com.epam.esm.webservice.validator.PaginationValidator;
import com.epam.esm.webservice.validator.impl.PaginationValidatorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.epam.esm.webservice.util.SortBy.NAME;
import static com.epam.esm.webservice.util.SortType.ASC;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {ServiceUtilConfig.class})
@ContextConfiguration(classes = {ModelMapper.class, PaginationValidatorImpl.class})
@ActiveProfiles("prod")
class CertificateServiceImplTest {

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private PaginationValidator validator;

    @Mock
    private CertificateRepository repository;

    private CertificateService service;

    @BeforeEach
    void setup() {
        service = new CertificateServiceImpl(repository, mapper, validator);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3, 4})
    void findAllWithParams_ShouldReturnLimitedAmount(int limit) {
        Pagination pagination = new Pagination(1, limit);
        CertificateParameters parameters = new CertificateParameters(null, NAME, ASC, "", pagination);
        List<Certificate> expected = emptyList();
        when(repository.findAllByParameters(parameters)).thenReturn(expected);
        assertEquals(expected.size(), service.findAllWithParams(parameters).size());
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -2, -3, -4, 0})
    void testGetAllByParameters_ShouldThrowInvalidPageNumberExceptionForEachParameter(Integer page) {
        Pagination pagination = new Pagination(page, 5);
        CertificateParameters parameters = new CertificateParameters(null, NAME, ASC, "", pagination);
        when(repository.countFilteredEntries(parameters)).thenReturn(1);
        assertThrows(InvalidPageNumberException.class, () -> service.findAllWithParams(parameters));
    }

    @Test
    void testFindById_ShouldReturnDto() {
        int id = 1;
        Certificate expected = new Certificate();
        when(repository.findById(id)).thenReturn(Optional.of(expected));
        assertEquals(mapper.map(expected, CertificateDTO.class), service.findById(id));
    }

    @Test
    void testFindById_ShouldThrowException() {
        when(repository.findById(1)).thenReturn(Optional.empty());
        assertThrows(CertificateNotFoundException.class, () -> service.findById(1));
    }

    @Test
    void testSave_ShouldConvertAndAdd() {
        CertificateDTO expected = new CertificateDTO();
        doNothing().when(repository).add(mapper.map(expected, Certificate.class));
        service.save(expected);
        when(repository.findById(1)).thenReturn(Optional.of(new Certificate()));
        assertEquals(expected, service.findById(1));
    }

    @Test
    void testSave_ShouldConvertAndUpdate() {
        CertificateDTO certificateDTO = new CertificateDTO();
        certificateDTO.setId(1);
        doNothing().when(repository).update(mapper.map(certificateDTO, Certificate.class));
        service.save(certificateDTO);
    }

    @Test
    void testDelete_ShouldReturnValueThenThrowResourceNotFoundException() {
        int id = 1;
        when(repository.findById(id)).thenReturn(Optional.of(new Certificate())).thenReturn(Optional.empty());
        assertNotNull(service.findById(id));
        service.deleteById(id);
        verify(repository).deleteById(id);
        assertThrows(ResourceNotFoundException.class, () -> service.findById(id));
    }
}
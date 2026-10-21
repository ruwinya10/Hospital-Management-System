package com.hms.reporting;
import com.hms.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import org.junit.jupiter.api.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;
import java.time.*;import java.math.BigDecimal;import java.util.*;
import static org.junit.jupiter.api.Assertions.*;import static org.mockito.Mockito.*;
class PharmacyMovementServiceTest {
 EntityManager em;PharmacyMovementService service;PharmacyItem medicine;
 @BeforeEach void setup(){em=mock(EntityManager.class);ObjectMapper json=new ObjectMapper();service=new PharmacyMovementService(em,json,new AnalyticsService(em,json));medicine=new PharmacyItem();medicine.id=1L;medicine.status="ACTIVE";medicine.createdAt=Instant.now().minusSeconds(100);medicine.details="{\"quantity\":10,\"unitPrice\":25,\"expiryDate\":\"2099-01-01\"}";when(em.find(PharmacyItem.class,1L,LockModeType.PESSIMISTIC_WRITE)).thenReturn(medicine);}
 PharmacyMovementService.MovementInput input(String type,String quantity){return new PharmacyMovementService.MovementInput(1L,type,new BigDecimal(quantity),null,null,"Test reference",null);}
 org.springframework.security.core.Authentication role(String r){return new UsernamePasswordAuthenticationToken("tester",null,List.of(new SimpleGrantedAuthority("ROLE_"+r)));}
 @Test void dispenseLocksStockAndPersistsMovementWithPriceSnapshot()throws Exception{var movement=service.record(input("DISPENSE","3"),role("PHARMACIST"));assertEquals("7",new ObjectMapper().readTree(medicine.details).path("quantity").asText());assertEquals(new BigDecimal("25"),movement.unitPrice);assertEquals("tester",movement.recordedBy);verify(em).persist(movement);verify(em).find(PharmacyItem.class,1L,LockModeType.PESSIMISTIC_WRITE);}
 @Test void insufficientStockDoesNotMutateOrPersist(){assertThrows(ResponseStatusException.class,()->service.record(input("DISPENSE","11"),role("ADMIN")));verify(em,never()).persist(any());assertTrue(medicine.details.contains("\"quantity\":10"));}
 @Test void expiredMedicineCannotBeDispensed(){medicine.details="{\"quantity\":10,\"expiryDate\":\"2000-01-01\"}";assertThrows(ResponseStatusException.class,()->service.record(input("DISPENSE","1"),role("PHARMACIST")));verify(em,never()).persist(any());}
 @Test void unauthorizedStaffCannotRecordMovements(){assertEquals(403,assertThrows(ResponseStatusException.class,()->service.record(input("RECEIPT","1"),role("NURSE"))).getStatusCode().value());verify(em,never()).find(any(),any(),any(LockModeType.class));}
 @Test void receiptIncreasesStock()throws Exception{service.record(input("RECEIPT","2.5"),role("ADMIN"));assertEquals("12.5",new ObjectMapper().readTree(medicine.details).path("quantity").asText());}
}

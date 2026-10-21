package com.hms.api;
import com.hms.reporting.*;
import com.hms.model.PharmacyMovement;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.time.LocalDate;
import java.util.*;
@RestController @RequestMapping("/api/reports/analytics")
public class AnalyticsController {
 private final AnalyticsService reports;private final PharmacyMovementService movements;
 public AnalyticsController(AnalyticsService r,PharmacyMovementService m){reports=r;movements=m;}
 @GetMapping public Map<String,Object> categories(Authentication a){return Map.of("categories",reports.categories(a));}
 @GetMapping("/{category}") public AnalyticsService.Report report(@PathVariable String category,@RequestParam(required=false) LocalDate from,@RequestParam(required=false) LocalDate to,@RequestParam(defaultValue="month") String groupBy,@RequestParam(required=false) Long patientId,@RequestParam(required=false) Long doctorId,@RequestParam(required=false) Long departmentId,@RequestParam(required=false) Long employeeId,@RequestParam(required=false) String status,@RequestParam(required=false) String search,Authentication a){reports.guard(a,category);return reports.generate(category,new AnalyticsService.Filter(from,to,groupBy,patientId,doctorId,departmentId,employeeId,status,search),a);}
 @PostMapping("/pharmacy/movements") public PharmacyMovement movement(@Valid @RequestBody PharmacyMovementService.MovementInput input,Authentication a){return movements.record(input,a);}
}

package com.ranafahad.NutriCheck.model;

import lombok.Data;
import java.util.Map;

@Data
public class NutritionData {
    private Map<String, Boolean> allergens;
    private Map<String, String> nutrition;
}
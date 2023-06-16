package com.example.dbaproject.api.models.processed_data;

import java.util.Date;
import java.util.List;

// Used only like dataclass that contains general fields
public class AbstractProcessedData {
    public Date dateCreation;
    public List<ProcessedDataCreate.Shape> shapes;

    public static class Shape {
        public String label;
        public double score;
        public List<Integer> bbox;
        public List<List<Integer>> polygon;
        public List<ProcessedDataCreate.Shape.Defect> defects;

        public static class Defect {
            public String name;
            public double score;
        }
    }
}

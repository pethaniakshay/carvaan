package com.codepuran.carvaan.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class FileSplitDto {
    private String name;
    private int firstPage;
    private int lastPage;
}

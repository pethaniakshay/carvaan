package com.codepuran.carvaan.dto;

import lombok.*;

import java.util.List;
import java.util.Set;

@Setter
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParsedSongsDto implements Cloneable{
    private Boolean filled;
    private String no;
    private String name;
    private String film;
    private String primaryArtiste;
    private String mood;
    private String rawArtistes;
    private String album;
    private Set<String> artistes;

    public Object clone() throws CloneNotSupportedException
    {
        return super.clone();
    }
}

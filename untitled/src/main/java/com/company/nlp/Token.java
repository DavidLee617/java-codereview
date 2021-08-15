package com.company.nlp;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Token {
    public  String term;
    public  int start;
    public int end;
}

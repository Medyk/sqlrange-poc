package com.example.sqlrange;

public record UpdateBookRequest(
        Integer revision,
        String title
) {}
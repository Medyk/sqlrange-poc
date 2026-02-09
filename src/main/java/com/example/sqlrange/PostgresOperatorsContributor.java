package com.example.sqlrange;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.type.StandardBasicTypes;

public class PostgresOperatorsContributor implements FunctionContributor {
    @Override
    public void contributeFunctions(FunctionContributions functionContributions) {
        // Rejestrujemy funkcję 'overlaps', która w SQL zamieni się na operator &&
        functionContributions.getFunctionRegistry().registerPattern(
                "overlaps",
                "(?1 && ?2)",
                functionContributions.getTypeConfiguration().getBasicTypeRegistry().resolve(StandardBasicTypes.BOOLEAN)
        );
        // Rejestrujemy funkcję 'contains', która w SQL zamieni się na operator @>
        functionContributions.getFunctionRegistry().registerPattern(
                "contains",
                "(?1 @> ?2)",
                functionContributions.getTypeConfiguration().getBasicTypeRegistry().resolve(StandardBasicTypes.BOOLEAN)
        );
    }
}

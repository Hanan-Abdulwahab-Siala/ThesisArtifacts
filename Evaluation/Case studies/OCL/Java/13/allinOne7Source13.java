// Enumerations -------------------
enum Season1 {
    WINTER, SPRING, SUMMER, AUTUMN
}

public enum Season2 {
    WINTER("Cold"), SPRING("Warmer"), SUMMER("Hot"), AUTUMN("Cooler");

    Season2(String description) {
        this.description = description;
    }

    private final String description;

    public String getDescription() {
        return description;
    }
}

public enum Season3 {
    WINTER {
        String getDescription() {return "cold";}
    },
    SPRING {
        String getDescription() {return "warmer";}
    },
    SUMMER {
        String getDescription() {return "hot";}
    },
    FALL {
        String getDescription() {return "cooler";}
    };
}


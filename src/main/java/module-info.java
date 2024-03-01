module alany.labb {
    requires javafx.controls;
    requires javafx.base;

    opens alany.labb to javafx.base;
    opens alany.labb.model to javafx.base; // open alany.labb.model package for reflection from PropertyValuesFactory (sigh ...)
    exports alany.labb;

    requires java.sql;
}
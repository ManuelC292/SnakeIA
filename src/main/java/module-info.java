module fr.manu.snakejava {
    requires javafx.controls;
    requires javafx.fxml;


    opens fr.manu.snakejava to javafx.fxml;
    exports fr.manu.snakejava;
}
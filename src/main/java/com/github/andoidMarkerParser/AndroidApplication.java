package com.github.andoidMarkerParser;

/**
 * Model of Android Application which is filled by AndroidMarketHandler
 *
 * @author blackbass
 */
public class AndroidApplication {

    private String name, image, packageName, description, detailsUrl, category, currency, minAndroidVersion;
    private Double price;
    private Long fileBytes;

    private AndroidApplication() {
    }

    /**
     * @param name        example: foo
     * @param image       example: http://some.foo.com/icon.png
     * @param packageName example: org.github.android.parser
     * @param description example: bla-bla-bla-bla
     * @param category    example: games
     * @param currency    example: USD
     * @param price       example: 1
     * @param fileBytes   example 1024
     */
    public AndroidApplication(String name, String image, String packageName,
                              String description, String category, String currency, Double price, Long fileBytes,
                              String minAnddroidVersion) {
        this.image = image;
        this.name = name;
        this.packageName = packageName;
        this.description = description;
        this.detailsUrl = AndroidMarketHandler.MARKET_URL_DETAILS_PAGE + packageName;
        this.category = category;
        this.currency = currency;
        this.price = price;
        this.fileBytes = fileBytes;
        this.minAndroidVersion = minAnddroidVersion;
    }

    /**
     * @return image url of application from Google Play
     */
    public String getImage() {
        return this.image;
    }

    /**
     * @return package name of applcation from Google Play
     */
    public String getPackageName() {
        return this.packageName;
    }

    /**
     * @return name of application from Google Play
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return description from Google Play
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * @return category string
     */
    public String getCategory() {
        return this.category;
    }

    /**
     * @return detailsURL string, example: https://play.google.com/store/apps/details?id=foo.bar.com
     */
    public String getDetailsUrl() {
        return this.detailsUrl;
    }

    /**
     * @return price Double
     */
    public Double getPrice() {
        return this.price;
    }

    /**
     * @return currency code String
     */
    public String getCurrency() {
        return this.currency;
    }

    /**
     * @return count of bytes of application on store in Long
     */
    public Long getFileBytes() {
        return fileBytes;
    }

    /**
     *
     * @return string of android version that needed on device
     */
    public String getMinAndroidVersion() {
        return this.minAndroidVersion;
    }

    public String toString() {
        return String.format("{\"name\": \"%s\", \"image\": \"%s\", \"packageName\": \"%s\", " +
                "\"description\": \"%s\", \"detailsUrl\": \"%s\", \"category\": \"%s\", \"currency\": \"%s\"," +
                "\"minAndroidVersion\": \"%s\", \"price\": \"%s\", \"fileBytes\": \"%s\"}",
                name, image, packageName, description, detailsUrl, category, currency, minAndroidVersion, price,
                fileBytes);
    }

}

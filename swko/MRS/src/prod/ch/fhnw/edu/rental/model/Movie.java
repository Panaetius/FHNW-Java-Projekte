package ch.fhnw.edu.rental.model;

import java.sql.Date;
import java.util.Calendar;

/**
 * Represents a movie.
 * 
 */
public class Movie {
    /**
     * unique movie id.
     */
    private int id;

    private String title;

    private boolean rented;

    private Date releaseDate;

    /**
     * the rental cost of the movie.
     */
    private PriceCategory priceCategory;

    /**
     * Ctor only for testing needed.
     */
    protected Movie() {
    }

    /**
     * @param aTitle none.
     * @param aPriceCategory none.
     */
    public Movie(String aTitle, PriceCategory aPriceCategory) {
        if ((aTitle == null) || (aPriceCategory == null)) {
            throw new NullPointerException("not all input parameters are set!");
        }
        this.title = aTitle;
        this.releaseDate = new Date(Calendar.getInstance().getTimeInMillis());
        this.priceCategory = aPriceCategory;
        this.rented = false;
    }

    /**
     * @param aTitle none.
     * @param aReleaseDate none.
     * @param aPriceCategory none.
     */
    public Movie(String aTitle, Date aReleaseDate, PriceCategory aPriceCategory) {
        if ((aTitle == null) || (aReleaseDate == null) || (aPriceCategory == null)) {
            throw new NullPointerException("not all input parameters are set!");
        }
        this.title = aTitle;
        this.releaseDate = aReleaseDate;
        this.priceCategory = aPriceCategory;
        this.rented = false;
    }

    /**
     * @return none.
     */
    public PriceCategory getPriceCategory() {
        return priceCategory;
    }

    /**
     * @param aPriceCategory none.
     */
    public void setPriceCategory(PriceCategory aPriceCategory) {
        this.priceCategory = aPriceCategory;
    }

    /**
     * @return none.
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param aTitle none.
     */
    public void setTitle(String aTitle) {
        if (this.title != null) {
            throw new IllegalStateException();
        }
        this.title = aTitle;
    }

    /**
     * @return none.
     */
    public Date getReleaseDate() {
        return releaseDate;
    }

    /**
     * @param aReleaseDate none.
     */
    public void setReleaseDate(Date aReleaseDate) {
        if (this.releaseDate != null) {
            throw new IllegalStateException();
        }
        this.releaseDate = aReleaseDate;
    }

    /**
     * @return none.
     */
    public boolean isRented() {
        return rented;
    }

    /**
     * @param isRented none.
     */
    public void setRented(boolean isRented) {
        this.rented = isRented;
    }

    /**
     * @return none.
     */
    public int getId() {
        return id;
    }

    /**
     * @param anId none.
     */
    public void setId(int anId) {
        this.id = anId;
    }

    /**
     * @see java.lang.Object#hashCode()
     * @return none.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime + id;
        result = prime * result + ((releaseDate == null) ? 0 : releaseDate.hashCode());
        result = prime * result + ((title == null) ? 0 : title.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     * @param obj none.
     * @return none.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || !(obj instanceof Movie)) {
            return false;
        }

        // declare other object to avoid casts in the following
        final Movie other = (Movie) obj;
        if (id != other.id) {
            return false;
        }

        if (releaseDate == null) {
          if (other.getReleaseDate() != null) {
              return false;
          }
        } else if (!releaseDate.equals(other.getReleaseDate())) {
          return false;
        }

        if (title == null) {
          if (other.getTitle() != null) {
              return false;
          }
        } else if (!title.equals(other.getTitle())) {
          return false;
        }

        return true;
    }
}

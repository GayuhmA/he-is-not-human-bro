package id.web.gayuhma.survival;

/**
 * Interface (Pilar OOP: Interface)
 * "Kontrak" bahwa setiap implementornya WAJIB bisa menerima damage dan mati.
 */
public interface IDamageable {
    void takeDamage(int amount);
    boolean isDead();
}

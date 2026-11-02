package um.tesoreria.sender.hexagonal.shared.infrastructure.support;

public interface Jsonifyable {

    default String jsonify() {
        return Jsonifier.builder(this).build();
    }

}

package um.tesoreria.sender.util;

public interface Jsonifyable {

    default String jsonify() {
        return Jsonifier.builder(this).build();
    }

}

package tree;

public class Node<T> { // get something like hashid from object interface
    T value;
    
    public Node(T _value) {
    	value = _value;
    }
    
    T getValue() {
    	return value;
    }

    public void toString(StringBuffer stringBuffer) {
        stringBuffer.append(value);
    }
}
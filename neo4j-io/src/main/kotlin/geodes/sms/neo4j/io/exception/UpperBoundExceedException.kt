package geodes.sms.neo4j.io.exception

class UpperBoundExceedException(id: Long, label: String, featureName: String, upperBound: Int, currentValue: Int) :
    Exception("Lover bound '$currentValue' exceeds the max possible value '$upperBound' for feature: '$featureName' in node: (id=$id label=$label)")

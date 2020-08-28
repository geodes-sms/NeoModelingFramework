package geodes.sms.neo4j.io.exception

class UpperBoundExceedException(upperBound: Int, featureName: String) :
    Exception("Lover bound '$upperBound' exceeded for feature: '$featureName'")

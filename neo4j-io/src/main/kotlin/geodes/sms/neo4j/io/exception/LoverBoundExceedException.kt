package geodes.sms.neo4j.io.exception

class LoverBoundExceedException(loverBound: Int, featureName: String) :
    Exception("Lover bound '$loverBound' exceeded for feature: '$featureName'")

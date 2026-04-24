package geodes.sms.neo4j.io.exception

class LoverBoundExceedException(id: Long, label: String, featureName: String, loverBound: Int, currentValue: Int) :
    Exception("Lover bound '$currentValue' does not reach the min possible value '$loverBound' for feature: '$featureName' in node: (id=$id label=$label)")

import java.util.concurrent.atomic.AtomicLong

class LogFilters {
    
    private static final AtomicLong REQUEST_NUMBER_COUNTER = new AtomicLong() 
    private static final String START_TIME_ATTRIBUTE = 'Controller__START_TIME__' 
    private static final String REQUEST_NUMBER_ATTRIBUTE = 'Controller__REQUEST_NUMBER__'
    private static final String MODEL_ATTRIBUTE = 'Controller__MODEL__'
    
    def filters = {
    
        logFilter(controller: '*', action: '*') {
    
            before = {
                if (!log.debugEnabled) return true

                long start = System.currentTimeMillis() 
                long requestNumber = REQUEST_NUMBER_COUNTER.incrementAndGet()

                request[START_TIME_ATTRIBUTE] = start 
                request[REQUEST_NUMBER_ATTRIBUTE] = requestNumber

                return true
            }

            after = { Map model ->
                if (!log.debugEnabled) return true

                request[MODEL_ATTRIBUTE] = model
                return true
            }

            afterView = { Exception e ->
                if (!log.debugEnabled) return true

                long start = request[START_TIME_ATTRIBUTE]
                long requestNumber = request[REQUEST_NUMBER_ATTRIBUTE]
                long duration = System.currentTimeMillis() - start 
                def model = request[MODEL_ATTRIBUTE]

                log.debug "${request.method} #$requestNumber (${duration}ms) ${request.remoteHost} - ${request.forwardURI} $model"
                return true
            }
        } 
    }
}
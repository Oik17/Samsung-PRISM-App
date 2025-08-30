    package com.prism.app.ml

    import com.prism.app.data.WifiFingerprint
    import kotlin.math.pow
    import kotlin.math.sqrt

    object SimpleKnn {
        // currentScan: map bssid->rssi ; fingerprints: list of WifiFingerprint rows
        fun predictRoom(currentScan: Map<String,Int>, fingerprints: List<WifiFingerprint>): String? {
            // aggregate fingerprints by room: build average vector per room
            val roomMap = mutableMapOf<String, MutableMap<String, MutableList<Int>>>()
            for (fp in fingerprints) {
                val map = roomMap.getOrPut(fp.roomId){ mutableMapOf() }
                map.getOrPut(fp.bssid){ mutableListOf() }.add(fp.rssi)
            }
            // average
            val roomAvg = roomMap.mapValues { (_, bmap) ->
                bmap.mapValues { (_, list) -> list.average().toFloat() }
            }

            // compute distance between currentScan and roomAvg -> only consider overlapping BSSIDs
            var best: Pair<String,Double>? = null
            for ((room, avgMap) in roomAvg) {
                var sum = 0.0
                var count = 0
                for ((bssid, rssiAvg) in avgMap) {
                    val cur = currentScan[bssid]
                    if (cur != null) {
                        val d = (cur - rssiAvg).toDouble()
                        sum += d.pow(2.0)
                        count++
                    }
                }
                if (count == 0) continue
                val dist = sqrt(sum / count)
                if (best == null || dist < best.second) best = room to dist
            }
            return best?.first
        }
    }

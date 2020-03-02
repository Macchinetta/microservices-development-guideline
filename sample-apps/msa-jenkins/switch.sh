#!/bin/bash
# ----------------------------------------------------------------------
# Copyright(c) 2019 NTT Corporation.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
# either express or implied. See the License for the specific language
# governing permissions and limitations under the License.
# ----------------------------------------------------------------------

TARGET_SERVICE=$1 # account, flight, flight-ticket-fare-calculation, purchase, purchase-notice, reserve, reserve-notice

MANIFESTSPATH="/var/lib/jenkins/workspace/msa-manifests"

printf "TARGET_SERVICE : ${TARGET_SERVICE}\n"

TARGET_ENV="prod"

CURRENT_ROLE=`kubectl get svc ${TARGET_SERVICE} -L role -n ${TARGET_ENV} | awk '{if(NR>1) print $7}'`

if [ "${CURRENT_ROLE}" == "green" ]; then
    TARGET_ROLE="blue"
else
    TARGET_ROLE="green"
fi

printf "TARGET_ROLE ======  ${TARGET_ROLE}\n"
printf "TARGET_ENV  ======  ${TARGET_ENV}\n"

TARGET_VERSION=`ls -al ${MANIFESTSPATH}/${TARGET_SERVICE}/ | awk 'END{if(NR>1) print $9}'`

cat "${MANIFESTSPATH}/${TARGET_SERVICE}/${TARGET_VERSION}/service.yml" | sed 's/\${TARGET_ROLE}'"/${TARGET_ROLE}/g" | kubectl apply -n ${TARGET_ENV} -f -
printf "Switch complete! "

sleep 1
kubectl get service -n ${TARGET_ENV} | grep -E "(NAME|${TARGET_SERVICE})"

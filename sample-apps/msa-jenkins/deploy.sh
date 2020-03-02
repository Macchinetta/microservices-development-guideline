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

if [ $# -lt 3 ]; then
    echo "引数が間違っています。" 1>&2
    echo "deploy.sh SERVICE VERSION ENV [local]" 1>&2
    exit 1
fi

TARGET_SERVICE=$1 # account, flight, flight-ticket-fare-calculation, purchase, purchase-notice, reserve, reserve-notice
TARGET_VERSION=$2
TARGET_ENV=$3
TARGET_MACHINE=$4

MANIFESTS_PATH="/var/lib/jenkins/workspace/msa-manifests"
AWS_REPO_PATH="307190070403.dkr.ecr.ap-northeast-1.amazonaws.com"

printf "TARGET_SERVICE : ${TARGET_SERVICE}\n"
printf "TARGET_VERSION : ${TARGET_VERSION}\n"
printf "TARGET_ENV : ${TARGET_ENV}\n"

if [ "${TARGET_ENV}" != "prod" ]; then
    TARGET_ENV="stag"
fi

TARGET_IMAGE=""
if [ "${TARGET_MACHINE}" == "local" ]; then
    TARGET_IMAGE="${TARGET_SERVICE}:${TARGET_VERSION}"
    MANIFESTS_PATH="./msa-manifests"
else
    TARGET_IMAGE="${AWS_REPO_PATH}\/${TARGET_SERVICE}:${TARGET_VERSION}"
fi

CURRENT_ROLE=`kubectl get svc ${TARGET_SERVICE} -L role -n ${TARGET_ENV} | awk '{if(NR>1) print $7}'`

if [ "${CURRENT_ROLE}" == "green" ]; then
    TARGET_ROLE="blue"
else
    TARGET_ROLE="green"
fi

printf "TARGET_ROLE  ======  ${TARGET_ROLE}\n"
printf "TARGET_ENV   ======  ${TARGET_ENV}\n"
printf "TARGET_IMAGE ======  ${TARGET_IMAGE}\n"

kubectl delete deploy ${TARGET_SERVICE}-${TARGET_ROLE} -n ${TARGET_ENV}
cat "${MANIFESTS_PATH}/${TARGET_SERVICE}/${TARGET_VERSION}/deployment.yml" | sed 's/\${TARGET_ROLE}'"/${TARGET_ROLE}/g" | sed 's/\${TARGET_IMAGE}'"/${TARGET_IMAGE}/g"| kubectl apply -n ${TARGET_ENV} -f - 

i=0
availableCnt=0
max_count=180

while [ $i -lt $max_count ];
do
    availableCnt=`kubectl get deploy ${TARGET_SERVICE}-${TARGET_ROLE} -n ${TARGET_ENV} | awk '{if(NR>1) print $4}'`
    printf "$i Available Count ======  ${availableCnt}\n"

    sleep 1

    if (( availableCnt > 0 )); then
        printf "Deployment apply success!  ${availableCnt}\n"
        break;
    fi

    i=$(( $i + 1 ))

done

if (( availableCnt > 0 )); then
    cat "${MANIFESTS_PATH}/${TARGET_SERVICE}/${TARGET_VERSION}/service.yml" | sed 's/\${TARGET_ROLE}'"/${TARGET_ROLE}/g" | kubectl apply -n ${TARGET_ENV} -f -
    printf "Deployment complete! "

    sleep 1
    kubectl get service -n ${TARGET_ENV} | grep -E "(NAME|${TARGET_SERVICE})"
    kubectl get po -n ${TARGET_ENV} | grep -E "(NAME|${TARGET_SERVICE})"
else
    printf "Deployment failed!!!  ${availableCnt}\n"
    exit 1
fi

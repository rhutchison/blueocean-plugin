/* eslint-disable */
import React from 'react';
import {storiesOf, action} from '@kadira/storybook';
import {
    ModalView,
    ModalBody,
    ModalHeader,
    PageTabs,
    Progress,
    TabLink,
} from '@jenkins-cd/design-language';

import {RunDetailsHeader} from '../RunDetailsHeader';

import {RunRecord} from '../records';

import {changeSet, currentRunRaw, pipeline} from './data/changesData';

const baseRun = new RunRecord(currentRunRaw);
const currentRun = baseRun.set('changeSet', changeSet.slice(0, 1));
const currentRunLong = baseRun.set('changeSet', changeSet.slice(0, 5));
const status = currentRun.getComputedResult() || '';

const strings = {
    "common.date.duration.format": "m[ minutes] s[ seconds]",
    "common.date.duration.hint.format": "M [month], d [days], h[h], m[m], s[s]",
    "common.date.readable.long": "MMM DD YYYY h:mma Z",
    "common.date.readable.short": "MMM DD h:mma Z",
    "rundetail.header.branch": "Branch",
    "rundetail.header.changes.names": "Changes by {0}",
    "rundetail.header.changes.none": "No changes",
    "rundetail.header.commit": "Commit",
};

const t = (key) => strings[key] || key;

storiesOf('Run Details Header', module)
    .add('Some changes', someChangesOld)
    .add('Lots of changes', lotsaChangesOld)
;

function someChangesOld() {
    return (
        <ModalView isVisible
                   transitionClass="expand-in"
                   transitionDuration={150}
                   result={status}>
            <ModalHeader>
                <div>
                    <RunDetailsHeader
                        locale="en"
                        t={t}
                        pipeline={pipeline}
                        data={currentRun}
                        onOrganizationClick={ action('button-click')}
                        onNameClick={ action('button-click')}
                        onAuthorsClick={ action('button-click')}
                    />
                </div>
            </ModalHeader>
        </ModalView>
    );
}

function lotsaChangesOld() {
    return (
        <ModalView isVisible
                   transitionClass="expand-in"
                   transitionDuration={150}
                   result={status}>
            <ModalHeader>
                <div>
                    <RunDetailsHeader t={t}
                                      locale="en"
                                      pipeline={pipeline}
                                      data={currentRunLong}
                                      onOrganizationClick={ action('button-click')}
                                      onNameClick={ action('button-click')}
                                      onAuthorsClick={ action('button-click')}/>
                </div>
            </ModalHeader>
        </ModalView>
    )
}

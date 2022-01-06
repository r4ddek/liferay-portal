/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Liferay Enterprise
 * Subscription License ("License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License by
 * contacting Liferay, Inc. See the License for the specific language governing
 * permissions and limitations under the License, including but not limited to
 * distribution rights of the Software.
 */

import Assignments from './Assignments';
import EdgeInformation from './EdgeInformation';
import NodeInformation from './NodeInformation';
import SelectAssignment from './SelectAssignment';

const sectionComponents = {
	assignments: Assignments,
	edgeInformation: EdgeInformation,
	nodeInformation: NodeInformation,
	selectAssignment: SelectAssignment,
};

export default sectionComponents;

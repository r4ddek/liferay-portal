/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

import {useMutation} from '@apollo/client';
import ClayButton from '@clayui/button';
import ClayForm, {ClayCheckbox} from '@clayui/form';
import ClayLayout from '@clayui/layout';
import {useState} from 'react';

import Input from '../../components/Input';
import Container from '../../components/Layout/Container';
import Modal from '../../components/Modal';
import {createUserAccount} from '../../graphql/mutations/liferayUser';
import {FormModalOptions} from '../../hooks/useFormModal';
import i18n from '../../i18n';
import {Liferay} from '../../services/liferay/liferay';

const userFormDefault = {
	alternateName: '',
	confirmPassword: '',
	emailAddress: '',
	familyName: '',
	givenName: '',
	password: '',
	testRayAnalyst: false,
	testrayAdministrator: false,
	testrayLead: false,
	testrayUser: false,
};

type CreateUserFormProps = {
	form: typeof userFormDefault;
	onChange: (event: any) => void;
};

const CreateUserForm: React.FC<CreateUserFormProps> = ({form, onChange}) => {
	return (
		<Container>
			<ClayForm>
				<ClayLayout.Row justify="start">
					<ClayLayout.Col size={12} sm={12} xl={3}>
						<h5 className="font-weight-normal mt-1">
							{i18n.translate('user-information')}
						</h5>
					</ClayLayout.Col>

					<ClayLayout.Col size={12} sm={12} xl={9}>
						<ClayForm.Group className="form-group-sm">
							<Input
								label={i18n.translate('first-name')}
								name="givenName"
								onChange={onChange}
								required
								value={form.givenName}
							/>

							<Input
								label={i18n.translate('last-name')}
								name="familyName"
								onChange={onChange}
								required
								value={form.familyName}
							/>

							<Input
								label={i18n.translate('email-address')}
								name="emailAddress"
								onChange={onChange}
								required
								type="email"
								value={form.emailAddress}
							/>

							<Input
								label={i18n.translate('screen-name')}
								name="alternateName"
								onChange={onChange}
								required
								value={form.alternateName}
							/>
						</ClayForm.Group>
					</ClayLayout.Col>
				</ClayLayout.Row>

				<hr />

				<ClayLayout.Row justify="start">
					<ClayLayout.Col size={12} sm={12} xl={3}>
						<h5 className="font-weight-normal mt-1">
							{i18n.translate('password')}
						</h5>
					</ClayLayout.Col>

					<ClayLayout.Col size={12} sm={12} xl={9}>
						<ClayForm.Group className="form-group-sm">
							<Input
								label={i18n.translate('password')}
								name="password"
								onChange={onChange}
								required
								type="password"
								value={form.password}
							/>

							<Input
								label="Confirm Password"
								name="repassword"
								onChange={onChange}
								required
								type="password"
								value={form.password}
							/>
						</ClayForm.Group>
					</ClayLayout.Col>
				</ClayLayout.Row>

				<hr />

				<ClayLayout.Row justify="start">
					<ClayLayout.Col size={12} sm={12} xl={3}>
						<h5 className="font-weight-normal">Roles</h5>
					</ClayLayout.Col>

					<ClayLayout.Col size={12} sm={12} xl={9}>
						<div>
							<ClayCheckbox
								checked={form.testrayAdministrator}
								label=" Testray Administrator"
								name="testrayAdministrator"
								onChange={onChange}
							/>
						</div>

						<div>
							<ClayCheckbox
								checked={form.testRayAnalyst}
								label="Testray Analyst"
								name="testRayAnalyst"
								onChange={onChange}
							/>
						</div>

						<div>
							<ClayCheckbox
								checked={form.testrayLead}
								label="Testray Lead"
								name="testrayLead"
								onChange={onChange}
							/>
						</div>

						<div>
							<ClayCheckbox
								checked={form.testrayUser}
								label="Testray User"
								name="testrayUser"
								onChange={onChange}
							/>
						</div>
					</ClayLayout.Col>
				</ClayLayout.Row>
			</ClayForm>
		</Container>
	);
};

type CreateUserProps = {
	modal: FormModalOptions;
};

const CreateUser: React.FC<CreateUserProps> = ({
	modal: {observer, onChange, onClose, onSave, visible},
}) => {
	const [form, setForm] = useState(userFormDefault);

	const [onCreateliferayAccount] = useMutation(createUserAccount);

	const onSubmit = async () => {
		const newForm: Partial<typeof userFormDefault> = {
			alternateName: form.alternateName,
			emailAddress: form.emailAddress,
			familyName: form.familyName,
			givenName: form.givenName,
			password: form.password,
		};

		try {
			await onCreateliferayAccount({
				variables: {
					userAccount: newForm,
				},
			});

			onSave();
		}
		catch (error) {
			Liferay.Util.openToast({
				message: (error as any).message,
				type: 'danger',
			});
		}
	};

	return (
		<Modal
			last={
				<ClayButton.Group spaced>
					<ClayButton displayType="secondary" onClick={onClose}>
						{i18n.translate('close')}
					</ClayButton>

					<ClayButton displayType="primary" onClick={onSubmit}>
						{i18n.translate('add-user-account')}
					</ClayButton>
				</ClayButton.Group>
			}
			observer={observer}
			size="lg"
			title={i18n.translate('new-user-account')}
			visible={visible}
		>
			<CreateUserForm form={form} onChange={onChange({form, setForm})} />
		</Modal>
	);
};

export default CreateUser;

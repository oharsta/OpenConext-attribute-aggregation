import styles from './_AuthorityConfiguration.scss';

import React from 'react';

import i18n from 'i18next';

import API from '../../util/API';
import Utils from '../../util/Utils';

const attributeKeys = ['caseExact', 'description', 'multiValued', 'mutability', 'required', 'returned', 'type', 'uniqueness']

export default class AuthorityConfiguration extends React.Component {

  constructor(props, context) {
    super(props, context);
    this.state = {
      authorities: [],
      selectedAuthority: {attributes: []}
    };
    API.getAuthorityConfiguration((json) => this.setState(
      {authorities: json.authorities, selectedAuthority: json.authorities[0]}
    ));
  }

  handleShowAuthority = (authority) => (e) => {
    Utils.stop(e);
    this.setState({selectedAuthority: authority})
  };

  renderAuthorityLink(authority) {
    var currentAuthority = this.state.selectedAuthority && this.state.selectedAuthority.id === authority.id;
    let style = currentAuthority ? styles.authority_selected : styles.authority_link;
    return currentAuthority ?
      <p key={authority.id} className={style}>{authority.id}</p> :
      <a key={authority.id} href="#" className={style}
         onClick={this.handleShowAuthority(authority)}>{authority.id}</a>;
  }

  renderAuthority() {
    var authority = this.state.selectedAuthority;
    return (
      <div className={styles.authority_details}>
        <section className={styles.header}>{authority.id}</section>
        <span>{i18n.t('authority.description')}</span>
        <p>{authority.description}</p>
        <span>{i18n.t('authority.endpoint')}</span>
        <p>{authority.endpoint}</p>
        <span>{i18n.t('authority.userName')}</span>
        <p>{authority.user}</p>
        {this.renderAttributes(authority.attributes)}
        <span/>
      </div>
    );
  }

  renderAttributes(attributes) {
    return (
      <div>
        {attributes.map(this.renderAttribute)}
      </div>
    )
  }

  renderAttribute(attribute) {
    const valueToString = (val) => val !== undefined && val !== null ? val.toString() : '';
    return (
      <div key={attribute.attributeAuthorityId + '-' + attribute.name}>
        <section className={styles.attribute}>{i18n.t('authority.attribute')}<em>{attribute.name}</em></section>
        {attributeKeys.map((key) =>
          <div key={attribute.name + '-' + key} className={styles.attributeDetails}>
            <span>{i18n.t('authority.' + key)}</span>
            <p>{valueToString(attribute[key])}</p>
          </div>)
        }
      </div>
    );
  }


  render() {
    return (
      <div className={styles.mod_container}>
        <div className={styles.mod_left_authorities}>
          <section className={styles.header}>{i18n.t('authority.authorities')}</section>
          {this.state.authorities.map((authority) => this.renderAuthorityLink(authority))}
        </div>
        <div className={styles.mod_right_authorities}>
          {this.renderAuthority()}
        </div>

      </div>
    );
  }
}

import React, {Component} from 'react';

import BASE_URL from '../../Constants';

import ListGroup from 'react-bootstrap/ListGroup';

import { withRouter } from 'react-router-dom';

import './Pages.css';

class Pages extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        const { transactionsLength, perRow, currentPage } = this.props;

        const pages = [];
        for (let i = 1; i <= Math.ceil(transactionsLength / perRow); i++) {
            pages.push(i);
        }

        const pagesList = pages.map(page => {
            return (
                <ListGroup.Item active={currentPage == page} variant="light" key={page} id={page} onClick={() => {this.props.handleClick(page)}}>
                    {page}
                </ListGroup.Item>
            );
        });
        
        return (
            <ListGroup id="page-list-group" horizontal>
                {pagesList}
            </ListGroup>
        );
    }
}

export default withRouter(Pages);
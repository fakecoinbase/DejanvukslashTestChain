import React, {Component} from 'react';

import Navbar, { Text } from 'react-bootstrap/Navbar';
import Button from 'react-bootstrap/Button';
import Nav from 'react-bootstrap/Nav';
import NavDropdown from 'react-bootstrap/NavDropdown';

import './Header.css';

class Header extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        return (<Navbar variant="dark" className="custom-Navbar" expand="lg">
            <Navbar.Brand >Test Chain</Navbar.Brand>
            <Navbar.Toggle aria-controls="basic-navbar-nav" />
            <Navbar.Collapse id="basic-navbar-nav">
                <Nav className="mr-auto">
                    <Nav.Link onClick={()=> {}}>Data</Nav.Link>
                </Nav>
            </Navbar.Collapse>
        </Navbar>);
    }
}

export default Header;